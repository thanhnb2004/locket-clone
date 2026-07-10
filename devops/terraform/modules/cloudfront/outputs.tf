# Re-exported with short names so callers (e.g. the route53 module's alias
# records and the s3 module's OAC bucket policy) can wire to them directly.

output "id" {
  description = "CloudFront distribution ID"
  value       = module.cloudfront.cloudfront_distribution_id
}

output "arn" {
  description = "CloudFront distribution ARN (pass to the s3 module for the OAC bucket policy)"
  value       = module.cloudfront.cloudfront_distribution_arn
}

output "domain_name" {
  description = "Distribution domain name (target for Route 53 alias records)"
  value       = module.cloudfront.cloudfront_distribution_domain_name
}

output "hosted_zone_id" {
  description = "CloudFront's fixed hosted zone ID (for Route 53 alias records)"
  value       = module.cloudfront.cloudfront_distribution_hosted_zone_id
}
