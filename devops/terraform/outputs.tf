output "app_url" {
  description = "URL to open the app"
  value       = local.use_custom_domain ? "https://${var.domain_name}" : "https://${aws_cloudfront_distribution.main.domain_name}"
}

output "cloudfront_domain_name" {
  description = "CloudFront distribution domain"
  value       = aws_cloudfront_distribution.main.domain_name
}

output "cloudfront_distribution_id" {
  description = "Use for cache invalidation after deploying the frontend"
  value       = aws_cloudfront_distribution.main.id
}

output "frontend_bucket" {
  description = "Upload the frontend build (frontend/dist) here"
  value       = aws_s3_bucket.frontend.bucket
}

output "moments_bucket" {
  description = "S3 bucket used by the backend for photo storage"
  value       = aws_s3_bucket.moments.bucket
}

output "alb_dns_name" {
  description = "ALB DNS name (only reachable through CloudFront)"
  value       = aws_lb.main.dns_name
}

output "rds_proxy_endpoint" {
  description = "RDS Proxy endpoint the backend connects to"
  value       = aws_db_proxy.main.endpoint
}

output "aurora_writer_endpoint" {
  description = "Aurora cluster writer endpoint"
  value       = aws_rds_cluster.main.endpoint
}

output "aurora_reader_endpoint" {
  description = "Aurora cluster reader endpoint"
  value       = aws_rds_cluster.main.reader_endpoint
}

output "ecs_cluster_name" {
  value = aws_ecs_cluster.main.name
}

output "db_secret_arn" {
  description = "Secrets Manager secret holding the DB credentials"
  value       = aws_secretsmanager_secret.db.arn
}
