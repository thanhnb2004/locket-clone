variable "project" {
  description = "Name prefix for all resources"
  type        = string
  default     = "locket"
}

variable "aws_region" {
  description = "AWS region for the main stack (VPC, ECS, Aurora, S3)"
  type        = string
  default     = "ap-southeast-1"
}

# ---------------------------------------------------------------------------
# Networking
# ---------------------------------------------------------------------------

variable "vpc_cidr" {
  description = "CIDR block of the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "az_count" {
  description = "Number of availability zones (public + private subnet pair per AZ)"
  type        = number
  default     = 2
}

# ---------------------------------------------------------------------------
# DNS / TLS (optional)
# ---------------------------------------------------------------------------

variable "domain_name" {
  description = <<-EOT
    Public domain for the app (e.g. locket.example.com). Leave empty to skip
    Route 53 + ACM and use the default *.cloudfront.net certificate/domain.
    The domain's hosted zone must already exist in Route 53.
  EOT
  type        = string
  default     = ""
}

variable "hosted_zone_name" {
  description = "Route 53 hosted zone name. Defaults to domain_name when empty (use when domain_name is a subdomain, e.g. zone example.com for locket.example.com)."
  type        = string
  default     = ""
}

# ---------------------------------------------------------------------------
# Backend (ECS)
# ---------------------------------------------------------------------------

variable "backend_image" {
  description = "Docker image for the Spring Boot backend"
  type        = string
  default     = "n3thanh/locket-app:latest"
}

variable "backend_container_port" {
  description = "Port the backend listens on"
  type        = number
  default     = 8080
}

variable "backend_cpu" {
  description = "Fargate task CPU units (256 = 0.25 vCPU)"
  type        = number
  default     = 512
}

variable "backend_memory" {
  description = "Fargate task memory in MiB"
  type        = number
  default     = 1024
}

variable "backend_desired_count" {
  description = "Number of backend tasks (spread across AZs and FARGATE/FARGATE_SPOT)"
  type        = number
  default     = 2
}

# ---------------------------------------------------------------------------
# Database (Aurora PostgreSQL + RDS Proxy)
# ---------------------------------------------------------------------------

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "locket"
}

variable "db_username" {
  description = "Master username"
  type        = string
  default     = "locket"
}

variable "db_engine_version" {
  description = "Aurora PostgreSQL engine version"
  type        = string
  default     = "16.6"
}

variable "db_instance_class" {
  description = "Instance class for the Aurora writer and replica"
  type        = string
  default     = "db.t4g.medium"
}
