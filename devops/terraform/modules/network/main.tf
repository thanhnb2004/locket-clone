locals {
  public_subnets  = [for i, _ in var.azs : cidrsubnet(var.cidr, 8, i)]
  private_subnets = [for i, _ in var.azs : cidrsubnet(var.cidr, 8, i + 10)]

  public_subnet_names  = [for i, az in var.azs : "${var.name}-subnet-public${i + 1}-${az}"]
  private_subnet_names = [for i, az in var.azs : "${var.name}-subnet-private${i + 1}-${az}"]
}

module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.16"

  name = "${var.name}-vpc"
  cidr = var.cidr

  azs             = var.azs
  public_subnets  = local.public_subnets
  private_subnets = local.private_subnets

  public_subnet_names  = local.public_subnet_names
  private_subnet_names = local.private_subnet_names

  enable_nat_gateway     = true
  single_nat_gateway     = false
  one_nat_gateway_per_az = true

  enable_dns_hostnames = true
  enable_dns_support   = true

  reuse_nat_ips       = true
  external_nat_ip_ids = aws_eip.nat[*].id

  igw_tags = {Name = "${var.name}-igw"}
}

resource "aws_eip" "nat" {
  count = length(var.azs)
  domain = "vpc"
  tags = { Name = "${var.name}-nat-eip-${count.index}" }
}