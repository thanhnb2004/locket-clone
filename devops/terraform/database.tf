# Aurora PostgreSQL cluster (writer + reader, one per AZ) fronted by RDS Proxy,
# exactly as in the diagram. Credentials live in Secrets Manager and are
# injected into ECS tasks + used by the proxy for authentication.

resource "random_password" "db" {
  length  = 32
  special = false
}

resource "aws_secretsmanager_secret" "db" {
  name_prefix             = "${var.project}-db-"
  description             = "Aurora master credentials for ${var.project}"
  recovery_window_in_days = 0 # dev-friendly: allow immediate re-create
}

resource "aws_secretsmanager_secret_version" "db" {
  secret_id = aws_secretsmanager_secret.db.id
  secret_string = jsonencode({
    username = var.db_username
    password = random_password.db.result
  })
}

resource "aws_db_subnet_group" "main" {
  name       = "${var.project}-db"
  subnet_ids = aws_subnet.private[*].id
}

# --- Aurora cluster ----------------------------------------------------------

resource "aws_rds_cluster" "main" {
  cluster_identifier = "${var.project}-aurora"
  engine             = "aurora-postgresql"
  engine_version     = var.db_engine_version

  database_name   = var.db_name
  master_username = var.db_username
  master_password = random_password.db.result

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.db.id]

  storage_encrypted = true

  # Dev/test settings - tighten for production.
  skip_final_snapshot     = true
  deletion_protection     = false
  backup_retention_period = 1
  apply_immediately       = true
}

# Instance 0 becomes the writer (primary), instance 1 the reader replica,
# spread across the two AZs.
resource "aws_rds_cluster_instance" "main" {
  count = 2

  identifier         = "${var.project}-aurora-${count.index}"
  cluster_identifier = aws_rds_cluster.main.id
  engine             = aws_rds_cluster.main.engine
  engine_version     = aws_rds_cluster.main.engine_version
  instance_class     = var.db_instance_class

  db_subnet_group_name = aws_db_subnet_group.main.name
  apply_immediately    = true
}

# --- RDS Proxy ---------------------------------------------------------------

data "aws_iam_policy_document" "rds_proxy_assume" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["rds.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "rds_proxy" {
  name_prefix        = "${var.project}-rds-proxy-"
  assume_role_policy = data.aws_iam_policy_document.rds_proxy_assume.json
}

data "aws_iam_policy_document" "rds_proxy" {
  statement {
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [aws_secretsmanager_secret.db.arn]
  }

  statement {
    actions   = ["kms:Decrypt"]
    resources = ["*"]

    condition {
      test     = "StringEquals"
      variable = "kms:ViaService"
      values   = ["secretsmanager.${var.aws_region}.amazonaws.com"]
    }
  }
}

resource "aws_iam_role_policy" "rds_proxy" {
  name_prefix = "${var.project}-rds-proxy-"
  role        = aws_iam_role.rds_proxy.id
  policy      = data.aws_iam_policy_document.rds_proxy.json
}

resource "aws_db_proxy" "main" {
  name           = "${var.project}-rds-proxy"
  engine_family  = "POSTGRESQL"
  role_arn       = aws_iam_role.rds_proxy.arn
  require_tls    = false
  idle_client_timeout = 1800

  vpc_subnet_ids         = aws_subnet.private[*].id
  vpc_security_group_ids = [aws_security_group.rds_proxy.id]

  auth {
    auth_scheme = "SECRETS"
    iam_auth    = "DISABLED"
    secret_arn  = aws_secretsmanager_secret.db.arn
  }

  depends_on = [aws_iam_role_policy.rds_proxy]
}

resource "aws_db_proxy_default_target_group" "main" {
  db_proxy_name = aws_db_proxy.main.name

  connection_pool_config {
    max_connections_percent = 90
  }
}

resource "aws_db_proxy_target" "main" {
  db_proxy_name         = aws_db_proxy.main.name
  target_group_name     = aws_db_proxy_default_target_group.main.name
  db_cluster_identifier = aws_rds_cluster.main.cluster_identifier

  depends_on = [aws_rds_cluster_instance.main]
}
