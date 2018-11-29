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
	public List<Khachhang> getKhachhang(){
		 Iterator<Khachhang> iterator= repository.findAll().iterator();
		 List<Khachhang> khachhang=new ArrayList<Khachhang>();
		 while(iterator.hasNext()){
			 khachhang.add(iterator.next());
		 }
		 return khachhang;
	}
	

	@GetMapping("/Khachhang/{id}")
	public Optional<Khachhang> getKhachhang(@PathVariable Integer id){
		return repository.findById(id);
	}
	
	
	@PutMapping("/Khachhang/{id}")
	   public Khachhang updateKhachhang(@PathVariable Integer id,@RequestBody Khachhang khachhang){
		   Optional<Khachhang> cus= repository.findById(id);
		   if(cus.isPresent()){
			   Khachhang c=cus.get();
			   c.setName(khachhang.getName());
		   return repository.save(c);
		   }
		   else
			   return null;
	   }
	
	@DeleteMapping("/Khachhang/{id}")
	   public String deleteKhachhang(@PathVariable Integer id){
		  repository.deleteById(id);
		  return "Document Deleted";
	   }

	


		

}