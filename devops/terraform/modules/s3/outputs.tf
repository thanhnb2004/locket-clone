output "bucket_id" {
  description = "Bucket name"
  value       = module.s3_bucket.s3_bucket_id
}

output "bucket_arn" {
  description = "Bucket ARN"
  value       = module.s3_bucket.s3_bucket_arn
}

output "bucket_regional_domain_name" {
  description = "Regional domain name — use as the CloudFront S3 origin domain_name"
  value       = module.s3_bucket.s3_bucket_bucket_regional_domain_name
}
