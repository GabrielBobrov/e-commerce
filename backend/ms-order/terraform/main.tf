# --- Recursos de Mensageria (SNS/SQS) ---

resource "aws_sns_topic" "order_events" {
  name = "order-events-topic"
}

resource "aws_sqs_queue" "order_events_dlq" {
  name = "order-events-queue-dlq"
}

resource "aws_sqs_queue" "order_events_queue" {
  name = "order-events-queue"
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.order_events_dlq.arn
    maxReceiveCount     = 3
  })
}

resource "aws_sns_topic_subscription" "order_events_sqs_target" {
  topic_arn = aws_sns_topic.order_events.arn
  protocol  = "sqs"
  endpoint  = aws_sqs_queue.order_events_queue.arn
}

# --- Parâmetros de Configuração (SSM Parameter Store) ---

resource "aws_ssm_parameter" "order_events_topic_arn" {
  name  = "/ms-order/${terraform.workspace}/sns/order-events-topic-arn"
  type  = "String"
  value = aws_sns_topic.order_events.arn
}

resource "aws_ssm_parameter" "order_events_queue_name" {
  name  = "/ms-order/${terraform.workspace}/sqs/order-events-queue-name"
  type  = "String"
  value = aws_sqs_queue.order_events_queue.name
}