variable "table_name" {
  description = "Name of the DynamoDB table"
  type        = string
}

variable "billing_mode" {
  description = "Billing mode: PAY_PER_REQUEST or PROVISIONED"
  type        = string
  default     = "PAY_PER_REQUEST"
}

variable "hash_key" {
  description = "Partition (hash) key attribute name"
  type        = string
}

variable "range_key" {
  description = "Sort (range) key attribute name; leave null for none"
  type        = string
  default     = null
}

variable "attributes" {
  description = "List of attribute definitions (name/type) for the keys"
  type = list(object({
    name = string
    type = string
  }))
}

variable "tags" {
  description = "Extra tags applied to the table"
  type        = map(string)
  default     = {}
}
