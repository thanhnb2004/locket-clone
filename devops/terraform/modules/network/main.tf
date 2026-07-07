locals {
  public_subnets  = [for i, _ in var.azs : cidrsubnet(var.cidr, 8, i)]
  private_subnets = [for i, _ in var.azs : cidrsubnet(var.cidr, 8, i + 10)]
}

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.16"

  name = "${var.name}-vpc"
  cidr = var.cidr

  azs             = var.azs
  public_subnets  = local.public_subnets
  private_subnets = local.private_subnets

  enable_nat_gateway     = true
  single_nat_gateway     = false
  one_nat_gateway_per_az = true

  enable_dns_hostnames = true
  enable_dns_support   = true

  reuse_nat_ips       = true
  external_nat_ip_ids = aws_eip.nat.*.id

  tags = { Name = "${var.name}-vpc" }
}

resource "aws_eip" "nat" {
  count = length(var.azs)
  vpc = true
  tags = { Name = "${var.name}-nat-eip-${count.index}" }
}