variable "region" {
  description = "AWS region for the stack (everything except the CloudFront ACM cert)"
  type        = string
}

variable "project" {
  description = "Project name, used as a resource name prefix"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

# --- Terraform state backend -------------------------------------------------

variable "tf_state_bucket_name" {
  description = "Name of the S3 bucket that stores Terraform state"
  type        = string
}

variable "tf_state_lock_table_name" {
  description = "Name of the DynamoDB table used for Terraform state locking"
  type        = string
}

variable "tf_state_versioning_enabled" {
  description = "Enable object versioning on the state bucket so state revisions can be rolled back"
  type        = bool
}

variable "tf_state_force_destroy" {
  description = "Allow the state bucket to be destroyed while it still contains objects. Keep false to avoid losing state."
  type        = bool
}

variable "tf_state_lock_billing_mode" {
  description = "Billing mode for the state-lock DynamoDB table (PAY_PER_REQUEST or PROVISIONED)"
  type        = string
}

# --- Network -----------------------------------------------------------------

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
}

variable "azs" {
  description = "Availability zones to spread the subnets across"
  type        = list(string)
}

# --- Frontend (S3 + CloudFront) ----------------------------------------------

variable "frontend_bucket_name" {
  description = "Name of the frontend static-site S3 bucket"
  type        = string
}

variable "force_destroy" {
  description = "Allow the frontend bucket to be destroyed while it still contains objects"
  type        = bool
}

variable "attach_cloudfront_policy" {
  description = "Attach the OAC bucket policy granting the CloudFront distribution read access to the frontend bucket"
  type        = bool
}

variable "cloudfront_price_class" {
  description = "CloudFront price class"
  type        = string
}

# --- DNS (Route 53 + ACM) ----------------------------------------------------

variable "zone_name" {
  description = "Hosted zone / apex domain"
  type        = string
}

variable "record_name" {
  description = "Subdomain label for the app, e.g. \"love\" -> love.qitrang.id.vn"
  type        = string
}

variable "create_zone" {
  description = "Create the hosted zone (true) or look up an existing one (false)"
  type        = bool
}

variable "wait_for_cert_validation" {
  description = "Block apply until the ACM certificate is validated"
  type        = bool
}
