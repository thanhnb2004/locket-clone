variable "zone_name" {
  description = "Hosted zone / domain to manage, e.g. \"example.com\""
  type        = string
}

variable "domain_name" {
  description = "Public FQDN for the app (alias -> CloudFront), e.g. \"locket.example.com\""
  type        = string
}

variable "create_zone" {
  description = "Create the hosted zone (true) or look up an existing one (false)"
  type        = bool
  default     = true
}

variable "cloudfront_domain_name" {
  description = "CloudFront distribution domain name to alias to"
  type        = string
}

variable "cloudfront_hosted_zone_id" {
  description = "CloudFront's fixed hosted zone ID (aws_cloudfront_distribution.*.hosted_zone_id)"
  type        = string
}

variable "tags" {
  description = "Extra tags applied to DNS resources"
  type        = map(string)
  default     = {}
}
