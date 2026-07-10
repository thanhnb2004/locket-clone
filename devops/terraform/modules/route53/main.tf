module "route" {
  source  = "terraform-aws-modules/route53/aws"
  version = "6.5.0"

  name        = var.zone_name
  create_zone = var.create_zone

  records = {
    cloudfront_ipv4= {
      name = var.record_name
      type = "A"
      alias = {
        name    = var.cloudfront_domain_name
        zone_id = var.cloudfront_hosted_zone_id
      }
    }
    cloudfront_ipv6 = {
      name = var.record_name
      type = "AAAA"
      alias = {
        name    = var.cloudfront_domain_name
        zone_id = var.cloudfront_hosted_zone_id
      }
    }
  }

  tags = var.tags
}
