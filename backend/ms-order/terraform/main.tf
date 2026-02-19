provider "aws" {
  region                      = var.aws_region
  access_key                  = var.aws_access_key
  secret_key                  = var.aws_secret_key
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    sns = "http://localhost:4566"
    sqs = "http://localhost:4566"
  }
}

resource "aws_sns_topic" "order_events" {
  name = "order-events-topic"
}

resource "aws_sqs_queue" "order_events_queue" {
  name = "order-events-queue"
}

resource "aws_sns_topic_subscription" "order_events_sqs_target" {
  topic_arn = aws_sns_topic.order_events.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.order_events_queue.arn
}
