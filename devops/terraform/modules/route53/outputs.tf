output "zone_id" {
  description = "Hosted zone ID"
  value       = local.zone_id
}

output "name_servers" {
  description = "Zone name servers (set these at your registrar when create_zone = true)"
  value       = var.create_zone ? module.zones.route53_zone_name_servers[var.zone_name] : null
}

output "certificate_arn" {
  description = "ARN of the validated ACM certificate (attach to CloudFront)"
  value       = module.acm.acm_certificate_arn
}

output "app_fqdn" {
  description = "Public FQDN of the app"
  value       = var.domain_name
}
