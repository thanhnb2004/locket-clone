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

variable "versioning_enabled" {
  description = "Enable object versioning. Leave off for the static-site bucket; turn on for the Terraform state bucket so state revisions can be rolled back."
  type        = bool
  default     = false
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
