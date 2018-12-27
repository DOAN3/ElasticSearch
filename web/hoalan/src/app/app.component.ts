import {Component, OnInit} from '@angular/core';
import { ApiService } from './api.service';
import {forEach} from '@angular/router/src/utils/collection';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  p: any = 1;
  public product: Array<object> = [];
  public cat: Array<object> = [];
  constructor(private apiService: ApiService ) {}
  ngOnInit(): void {
    this.getProduct();
    this.getCat();
  }

  public getProduct() {
    this.apiService.getProduct().subscribe((data: Array<object>) => {
      this.product = data;
      console.log(data);
      // data.forEach((e) => {
      //   console.log(e.Ten.length);
      // });
    });
  }
  public getCat() {
    this.apiService.getCat().subscribe((Catdata: Array<object>) => {
      this.cat = Catdata;
      console.log(Catdata);
    });
  }
}
