# Multi-AZ ECS cluster running the Spring Boot backend on a mix of
# FARGATE (baseline, on-demand) and FARGATE_SPOT (cheap extra capacity),
# matching the diagram.

resource "aws_ecs_cluster" "main" {
  name = "${var.project}-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name       = aws_ecs_cluster.main.name
  capacity_providers = ["FARGATE", "FARGATE_SPOT"]
}

resource "aws_cloudwatch_log_group" "backend" {
  name              = "/ecs/${var.project}-backend"
  retention_in_days = 7
}

# --- IAM ----------------------------------------------------------------------

data "aws_iam_policy_document" "ecs_tasks_assume" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

# Execution role: pull image, write logs, read the DB secret for injection.
resource "aws_iam_role" "task_execution" {
  name_prefix        = "${var.project}-ecs-exec-"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume.json
}

resource "aws_iam_role_policy_attachment" "task_execution" {
  role       = aws_iam_role.task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

data "aws_iam_policy_document" "task_execution_secrets" {
  statement {
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [aws_secretsmanager_secret.db.arn]
  }
}

resource "aws_iam_role_policy" "task_execution_secrets" {
  name_prefix = "${var.project}-ecs-exec-secrets-"
  role        = aws_iam_role.task_execution.id
  policy      = data.aws_iam_policy_document.task_execution_secrets.json
}

# Task role: what the app itself may do - read/write the moments bucket.
resource "aws_iam_role" "task" {
  name_prefix        = "${var.project}-ecs-task-"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume.json
}

data "aws_iam_policy_document" "task_s3" {
  statement {
    actions = [
      "s3:GetObject",
      "s3:PutObject",
      "s3:DeleteObject",
    ]
    resources = ["${aws_s3_bucket.moments.arn}/*"]
  }

  statement {
    actions   = ["s3:ListBucket", "s3:GetBucketLocation"]
    resources = [aws_s3_bucket.moments.arn]
  }
}

resource "aws_iam_role_policy" "task_s3" {
  name_prefix = "${var.project}-ecs-task-s3-"
  role        = aws_iam_role.task.id
  policy      = data.aws_iam_policy_document.task_s3.json
}

# --- Task definition -----------------------------------------------------------

resource "aws_ecs_task_definition" "backend" {
  family                   = "${var.project}-backend"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.backend_cpu
  memory                   = var.backend_memory
  execution_role_arn       = aws_iam_role.task_execution.arn
  task_role_arn            = aws_iam_role.task.arn

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }

  container_definitions = jsonencode([
    {
      name      = "backend"
      image     = var.backend_image
      essential = true

      portMappings = [
        {
          containerPort = var.backend_container_port
          protocol      = "tcp"
        }
      ]

      environment = [
        # Connect through RDS Proxy, not directly to Aurora.
        { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://${aws_db_proxy.main.endpoint}:5432/${var.db_name}" },
        { name = "SPRING_DATASOURCE_USERNAME", value = var.db_username },

        # Real S3: empty endpoint/keys -> AWS SDK default chain (task role).
        { name = "APP_S3_ENDPOINT", value = "" },
        { name = "APP_S3_REGION", value = var.aws_region },
        { name = "APP_S3_BUCKET", value = aws_s3_bucket.moments.bucket },
        { name = "APP_S3_ACCESS_KEY", value = "" },
        { name = "APP_S3_SECRET_KEY", value = "" },
        { name = "APP_S3_PATH_STYLE_ACCESS", value = "false" },
      ]

      secrets = [
        {
          name      = "SPRING_DATASOURCE_PASSWORD"
          valueFrom = "${aws_secretsmanager_secret.db.arn}:password::"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.backend.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "backend"
        }
      }
    }
  ])
}

# --- Service --------------------------------------------------------------------

resource "aws_ecs_service" "backend" {
  name            = "${var.project}-backend"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.backend.arn
  desired_count   = var.backend_desired_count

  # Keep at least one task on regular Fargate; scale extras on cheap Spot.
  capacity_provider_strategy {
    capacity_provider = "FARGATE"
    base              = 1
    weight            = 1
  }

  capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
    weight            = 3
  }

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.backend.arn
    container_name   = "backend"
    container_port   = var.backend_container_port
  }

  health_check_grace_period_seconds = 120

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  depends_on = [
    aws_lb_listener.http,
    aws_db_proxy_target.main,
    aws_ecs_cluster_capacity_providers.main,
  ]
}
