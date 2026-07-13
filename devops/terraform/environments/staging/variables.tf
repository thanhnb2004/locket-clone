variable "region" {
  description = "AWS region for the stack (everything except the CloudFront ACM cert)"
  type        = string
  default     = "ap-southeast-1"
}

variable "project" {
  description = "Project name, used as a resource name prefix"
  type        = string
  default     = "locket-clone"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "staging"
}

# --- Network -----------------------------------------------------------------

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "azs" {
  description = "Availability zones to spread the subnets across"
  type        = list(string)
  default     = ["ap-southeast-1a", "ap-southeast-1b"]
}

# --- Frontend (S3 + CloudFront) ----------------------------------------------

variable "frontend_bucket_name" {
  description = "Name of the frontend static-site S3 bucket"
  type        = string
  default     = "love-qitrang-staging-frontend"
}

variable "force_destroy" {
  description = "Allow the frontend bucket to be destroyed while it still contains objects"
  type        = bool
  default     = true
}

variable "cloudfront_price_class" {
  description = "CloudFront price class"
  type        = string
  default     = "PriceClass_200"
}

# --- DNS (Route 53 + ACM) ----------------------------------------------------

variable "zone_name" {
  description = "Hosted zone / apex domain"
  type        = string
  default     = "qitrang.id.vn"
}

variable "record_name" {
  description = "Subdomain label for the app, e.g. \"love\" -> love.qitrang.id.vn"
  type        = string
  default     = "love"
}

variable "create_zone" {
  description = "Create the hosted zone (true) or look up an existing one (false)"
  type        = bool
  default     = true
}
