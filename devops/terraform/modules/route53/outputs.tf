output "zone_id" {
  description = "Hosted zone ID (pass to the cert-manager module for DNS validation)"
  value       = module.route.id
}

output "name_servers" {
  description = "Zone name servers (set these at your registrar when create_zone = true)"
  value       = module.route.name_servers
}

output "app_fqdn" {
  description = "Public FQDN of the app"
  value       = "${var.record_name}.${var.zone_name}"
}
