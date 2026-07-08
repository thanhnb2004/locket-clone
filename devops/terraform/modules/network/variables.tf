variable "name" {
  description = "Name prefix for network resources (e.g. \"locket\")"
  type        = string
}

variable "cidr" {
  description = "CIDR block of the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "azs" {
  description = "Availability zones to spread subnets across"
  type        = list(string)
}