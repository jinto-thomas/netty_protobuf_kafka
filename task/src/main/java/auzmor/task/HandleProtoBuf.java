package auzmor.task;

import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import auzmor.kafka.MyKafkaProducer;
import auzmor.protobuf.EmployeeProto.Employee;

public class HandleProtoBuf {
	
	public void addToProtobuf(String ss) throws Exception {
	
		JSONParser jp = new JSONParser();
		
		JSONObject data = (JSONObject)jp.parse(ss);
		System.out.println(ss + " " + data.toString());
		
		String name = (String) data.get("name");
		String id = (String)data.get("id");
		String sal = (String)data.get("salary");
		
		System.out.println(name + " " + id + " " + sal);
		Employee emp = Employee.newBuilder()
				.setName(name)
				.setId(Integer.parseInt(id))
				.setSalary(Double.parseDouble(sal))
				.build();

// check if is all data are intact		
//	    try {
//	        FileOutputStream output = new FileOutputStream("/home/jinto/waste/proto/test.ser");
//	        emp.writeTo(output);
//	        output.close();
//	 
//	        Employee empFromFile = Employee.parseFrom(new FileInputStream("/home/jinto/waste/proto/test.ser"));
//	        System.out.println(empFromFile);
//	     } catch (IOException e) {
//	             e.printStackTrace();
//	     }
	    
	    MyKafkaProducer.getInstance().sendToQueue(emp);
	}
		
}
