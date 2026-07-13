output "app_url" {
  description = "Public URL of the app"
  value       = "https://${local.app_fqdn}"
}

# --- Network -----------------------------------------------------------------

output "vpc_id" {
  description = "ID of the VPC"
  value       = module.network.vpc_id
}

output "public_subnet_ids" {
  description = "Public subnet IDs"
  value       = module.network.public_subnet_ids
}

output "private_subnet_ids" {
  description = "Private subnet IDs"
  value       = module.network.private_subnet_ids
}

# --- Frontend ----------------------------------------------------------------

output "frontend_bucket" {
  description = "Frontend S3 bucket name (target for `aws s3 sync`)"
  value       = module.s3.bucket_id
}

output "cloudfront_distribution_id" {
  description = "CloudFront distribution ID (target for cache invalidations)"
  value       = module.cloudfront.id
}

output "cloudfront_domain_name" {
  description = "CloudFront distribution domain name"
  value       = module.cloudfront.domain_name
}

# --- DNS ---------------------------------------------------------------------

output "certificate_arn" {
  description = "ARN of the validated ACM certificate"
  value       = module.cert_manager.certificate_arn
}

output "route53_name_servers" {
  description = "Zone name servers — set these at the registrar when create_zone = true"
  value       = module.route53.name_servers
}
