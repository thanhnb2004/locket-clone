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

variable "cloudfront_distribution_arn" {
  description = "ARN of the CloudFront distribution allowed to read this bucket via OAC. When empty, no bucket policy is attached (first-apply / standalone use)."
  type        = string
  default     = ""
}

variable "tags" {
  description = "Extra tags applied to the bucket"
  type        = map(string)
  default     = {}
}
