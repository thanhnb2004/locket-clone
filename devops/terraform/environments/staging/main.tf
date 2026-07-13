locals {
  name_prefix = "${var.project}-${var.environment}"
  app_fqdn    = "${var.record_name}.${var.zone_name}"

  common_tags = {
    Project     = var.project
    Environment = var.environment
    ManagedBy   = "terraform"
  }
}

# Terraform state backend: reuses the S3 module (versioning on) plus a
# DynamoDB table for state locking.
module "tf_state" {
  source = "../../modules/s3"

  bucket_name        = var.tf_state_bucket_name
  versioning_enabled = var.tf_state_versioning_enabled
  force_destroy      = var.tf_state_force_destroy

  tags = local.common_tags
}

module "tf_state_lock" {
  source = "../../modules/dynamodb"

  table_name   = var.tf_state_lock_table_name
  billing_mode = var.tf_state_lock_billing_mode
  hash_key     = "LockID"

  attributes = [
    {
      name = "LockID"
      type = "S"
    }
  ]

  tags = local.common_tags
}

module "network" {
  source = "../../modules/network"

  name = local.name_prefix
  cidr = var.vpc_cidr
  azs  = var.azs
}

module "route53" {
  source = "../../modules/route53"

  zone_name   = var.zone_name
  record_name = var.record_name
  create_zone = var.create_zone

  cloudfront_domain_name    = module.cloudfront.domain_name
  cloudfront_hosted_zone_id = module.cloudfront.hosted_zone_id

  tags = local.common_tags
}

module "cert_manager" {
  source = "../../modules/cert-manager"

  providers = {
    aws = aws.us_east_1
  }

  domain_name         = local.app_fqdn
  zone_id             = module.route53.zone_id
  wait_for_validation = var.wait_for_cert_validation

  tags = local.common_tags
}

module "s3" {
  source = "../../modules/s3"

  bucket_name                 = var.frontend_bucket_name
  force_destroy               = var.force_destroy
  attach_cloudfront_policy    = var.attach_cloudfront_policy
  cloudfront_distribution_arn = module.cloudfront.arn

  tags = local.common_tags
}

module "cloudfront" {
  source = "../../modules/cloudfront"

  aliases     = [local.app_fqdn]
  price_class = var.cloudfront_price_class

  s3_bucket_regional_domain_name = module.s3.bucket_regional_domain_name
  acm_certificate_arn            = module.cert_manager.certificate_arn

  tags = local.common_tags
}
