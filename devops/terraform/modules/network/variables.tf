variable "name" {
  type        = string
  default = "locket-clone"
}

variable "cidr" {
  type        = string
  default     = "10.0.0.0/16"
}

variable "azs" {
  type        = list(string)
  default     = ["ap-southeast-1a", "ap-southeast-1b"]
}