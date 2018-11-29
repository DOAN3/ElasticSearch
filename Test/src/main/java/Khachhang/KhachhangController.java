package Khachhang;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class KhachhangController {
	
	@Autowired
	KhachhangRepository repository;
	
	
	
	
	@PostMapping("Khachhang/add")
	public Khachhang addKhachhang(@RequestBody Khachhang khachhang ){
		return repository.save(khachhang);
	}
	
	@GetMapping("/Khachhang/all")
	public List<Khachhang> getStudents(){
		 Iterator<Khachhang> iterator= repository.findAll().iterator();
		 List<Khachhang> khachhang=new ArrayList<Khachhang>();
		 while(iterator.hasNext()){
			 khachhang.add(iterator.next());
		 }
		 return khachhang;
	}
	

	@GetMapping("/Khachhang/{id}")
	public Optional<Khachhang> getStudent(@PathVariable Integer id){
		return repository.findById(id);
	}
	
	
	@PutMapping("/student/{id}")
	   public Khachhang updateStudent(@PathVariable Integer id,@RequestBody Khachhang student){
		   Optional<Khachhang> std= repository.findById(id);
		   if(std.isPresent()){
			   Khachhang s=std.get();
			   s.setName(student.getName());
		   return repository.save(s);
		   }
		   else
			   return null;
	   }
	
	@DeleteMapping("/student/{id}")
	   public String deleteStudent(@PathVariable Integer id){
		  repository.deleteById(id);
		  return "Document Deleted";
	   }

	


		

}