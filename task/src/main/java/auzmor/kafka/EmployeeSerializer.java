package auzmor.kafka;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import auzmor.protobuf.EmployeeProto.Employee;

public class EmployeeSerializer implements Serializer<Employee> {
	
	@Override
	public byte[] serialize(final String topic, final Employee data) {
		return data.toByteArray();
	}

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {

	}

	@Override
	public void close() {

	}

}
