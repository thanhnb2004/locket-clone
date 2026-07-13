variable "bucket_name" {
  description = "Name of the frontend static-site bucket"
  type        = string
  default     = "love-qitrang-storage-bucket"
}

variable "force_destroy" {
  description = "Allow the bucket to be destroyed while it still contains objects"
  type        = bool
  default     = true
}

variable "attach_cloudfront_policy" {
  description = "Attach the OAC bucket policy granting the CloudFront distribution read access. This is the plan-time toggle; set true whenever cloudfront_distribution_arn is wired in (even if the ARN is only known after apply)."
  type        = bool
  default     = false
}

variable "cloudfront_distribution_arn" {
  description = "ARN of the CloudFront distribution allowed to read this bucket via OAC. Used in the policy body only; may be an unknown (computed) value at plan time."
  type        = string
  default     = ""
}

variable "tags" {
  description = "Extra tags applied to the bucket"
  type        = map(string)
  default     = {}
}
