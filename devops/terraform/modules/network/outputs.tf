output "vpc_id" {
  description = "ID of the VPC"
  value       = module.vpc.vpc_id
}

output "vpc_cidr_block" {
  description = "CIDR block of the VPC"
  value       = module.vpc.vpc_cidr_block
}

output "public_subnet_ids" {
  description = "Public subnet IDs (ALB, NAT), one per AZ"
  value       = module.vpc.public_subnets
}

output "private_subnet_ids" {
  description = "Private subnet IDs (ECS, Aurora, RDS Proxy), one per AZ"
  value       = module.vpc.private_subnets
}

output "nat_gateway_public_ips" {
  description = "Public IPs of the NAT Gateways (one per AZ)"
  value       = module.vpc.nat_public_ips
}

output "azs" {
  description = "Availability zones the subnets are spread across"
  value       = var.azs
}
