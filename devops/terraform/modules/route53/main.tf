module "zones" {
  source  = "terraform-aws-modules/route53/aws//modules/zones"
  version = "6.5.0"

  zones = var.create_zone ? {
    (var.zone_name) = {
      comment = "${var.zone_name} - managed by terraform"
    }
  } : {}

  tags = var.tags
}

data "aws_route53_zone" "existing" {
  count = var.create_zone ? 0 : 1

  name         = var.zone_name
  private_zone = false
}

locals {
  zone_id = var.create_zone ? module.zones.route53_zone_zone_id[var.zone_name] : data.aws_route53_zone.existing[0].zone_id
}

# --- Certificate (us-east-1, DNS-validated) ----------------------------------

module "acm" {
  source  = "terraform-aws-modules/acm/aws"
  version = "~> 5.0"

  providers = {
    aws = aws.us_east_1
  }

  domain_name       = var.domain_name
  zone_id           = local.zone_id
  validation_method = "DNS"

  wait_for_validation = true

  tags = var.tags
}

# --- Alias records -> CloudFront ---------------------------------------------

resource "aws_route53_record" "app_a" {
  zone_id = local.zone_id
  name    = var.domain_name
  type    = "A"

  alias {
    name                   = var.cloudfront_domain_name
    zone_id                = var.cloudfront_hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "app_aaaa" {
  zone_id = local.zone_id
  name    = var.domain_name
  type    = "AAAA"

  alias {
    name                   = var.cloudfront_domain_name
    zone_id                = var.cloudfront_hosted_zone_id
    evaluate_target_health = false
  }
}
