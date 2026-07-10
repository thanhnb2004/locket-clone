output "certificate_arn" {
  description = "ARN of the validated ACM certificate (attach to CloudFront viewer_certificate)"
  value       = module.acm.acm_certificate_arn
}

output "certificate_status" {
  description = "Certificate status (e.g. ISSUED)"
  value       = module.acm.acm_certificate_status
}
