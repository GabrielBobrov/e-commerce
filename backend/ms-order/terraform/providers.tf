# providers.tf

# Configuração do backend (armazenamento do estado)
# Ao usar workspaces, o Terraform automaticamente gerencia arquivos de estado separados
# no diretório 'terraform.tfstate.d/'. Não é necessário especificar o caminho.
terraform {
  backend "local" {}
}

# Configuração do provedor AWS
provider "aws" {
  region     = var.aws_region
  access_key = "test"
  secret_key = "test"

  # Ignora validações que não são necessárias para o LocalStack
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  # --- Lógica Condicional para Endpoints ---
  # Se o workspace for "local", aponta para o LocalStack.
  # Caso contrário, usa os endpoints padrão da AWS (valor nulo).
  endpoints {
    sns         = terraform.workspace == "local" ? "http://localhost:4566" : null
    sqs         = terraform.workspace == "local" ? "http://localhost:4566" : null
    rds         = terraform.workspace == "local" ? "http://localhost:4566" : null
    elasticache = terraform.workspace == "local" ? "http://localhost:4566" : null
  }
}