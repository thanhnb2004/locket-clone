variable "domain_name" {
  description = "Primary domain for the certificate, e.g. \"love.qitrang.id.vn\""
  type        = string
  default     = "love.qitrang.id.vn"
}

variable "subject_alternative_names" {
  description = "Additional domains covered by the certificate"
  type        = list(string)
  default     = []
}

variable "zone_id" {
  description = "Route 53 hosted zone ID in which to create the DNS validation records"
  type        = string
}

variable "wait_for_validation" {
  description = "Wait for the certificate to be issued/validated before completing"
  type        = bool
  default     = true
}

variable "tags" {
  description = "Extra tags applied to the certificate"
  type        = map(string)
  default     = {}
}
