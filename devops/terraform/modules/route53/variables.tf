variable "zone_name" {
  description = "Hosted zone / apex domain, e.g. \"qitrang.id.vn\""
  type        = string
  default     = "qitrang.id.vn"
}

variable "record_name" {
  description = "Subdomain label for the app alias records, e.g. \"love\" -> love.qitrang.id.vn"
  type        = string
  default     = "love"
}

variable "create_zone" {
  description = "Create the hosted zone (true) or look up an existing one (false)"
  type        = bool
  default     = true
}

variable "cloudfront_domain_name" {
  description = "CloudFront distribution domain name to alias to (module.cloudfront.domain_name)"
  type        = string
}

variable "cloudfront_hosted_zone_id" {
  description = "CloudFront's fixed hosted zone ID (module.cloudfront.hosted_zone_id)"
  type        = string
}

variable "tags" {
  description = "Extra tags applied to DNS resources"
  type        = map(string)
  default     = {}
}
