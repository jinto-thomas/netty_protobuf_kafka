package auzmor.kafka;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import auzmor.protobuf.EmployeeProto.Employee;

public class MyKafkaProducer {

	private static MyKafkaProducer instance = null;
	private static Producer<String, Employee> producer = null;

	private final static String TOPIC = "employee-stuff";

	private MyKafkaProducer() {
		System.out.println("Starting singleton kafka producer for server");

		Properties props = new Properties();

		props.put("bootstrap.servers", "localhost:9092");
		props.put("producer.type", "async");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		
		producer = new KafkaProducer<>(props, new StringSerializer(), new EmployeeSerializer());
	}

	public static MyKafkaProducer getInstance() {
		if (instance == null)
			instance = new MyKafkaProducer();

		return instance;
	}

	public static void sendToQueue(Employee emp) throws Exception {
		producer.send(new ProducerRecord<String, Employee>(TOPIC, emp));
	}

}
