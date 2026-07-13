output "table_name" {
  description = "DynamoDB table name"
  value       = module.dynamodb_table.dynamodb_table_id
}

output "table_arn" {
  description = "DynamoDB table ARN"
  value       = module.dynamodb_table.dynamodb_table_arn
}
