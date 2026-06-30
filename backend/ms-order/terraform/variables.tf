# variables.tf

variable "aws_region" {
  description = "A região da AWS onde os recursos serão criados."
  type        = string
  default     = "us-east-1"
}

variable "db_password" {
  description = "A senha para o banco de dados RDS."
  type        = string
  sensitive   = true # O Terraform não mostrará o valor no console
}