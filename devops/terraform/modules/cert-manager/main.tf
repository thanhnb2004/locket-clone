module "acm" {
  source  = "terraform-aws-modules/acm/aws"
  version = "6.3.0"

  domain_name               = var.domain_name
  subject_alternative_names = var.subject_alternative_names
  zone_id                   = var.zone_id

  validation_method      = "DNS"
  create_route53_records = true
  wait_for_validation    = var.wait_for_validation

  tags = var.tags
}
