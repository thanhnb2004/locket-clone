variable "aliases" {
  description = "Custom domain aliases (CNAMEs) for the distribution"
  type        = list(string)
  default     = ["love.qitrang.id.vn"]
}

variable "s3_bucket_regional_domain_name" {
  description = "Regional domain name of the S3 frontend bucket (module.s3.bucket_regional_domain_name)"
  type        = string
}

variable "acm_certificate_arn" {
  description = "ARN of the ACM certificate (issued in us-east-1) for the aliases"
  type        = string
}

variable "price_class" {
  description = "CloudFront price class"
  type        = string
  default     = "PriceClass_200"
}

variable "cache_policy_id" {
  description = "Managed cache policy ID for the default behavior (defaults to AWS CachingOptimized)"
  type        = string
  default     = "658327ea-f89d-4fab-a63d-7e88639e58f6"
}

variable "tags" {
  description = "Extra tags applied to the distribution"
  type        = map(string)
  default     = {}
}
