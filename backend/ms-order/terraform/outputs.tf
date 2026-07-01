# outputs.tf

output "order_events_topic_arn" {
  description = "O ARN do tópico SNS para eventos de pedido."
  value       = aws_sns_topic.order_events.arn
}

output "order_events_queue_name" {
  description = "O nome da fila SQS principal."
  value       = aws_sqs_queue.order_events_queue.name
}