import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})

export class ApiService {
  constructor(private httpClient: HttpClient, private CathttpClient: HttpClient) {}
  getProduct() {
    return this.httpClient.get(environment.server + '/items/all');
  }
  getCat() {
    return this.CathttpClient.get(environment.server + '/categories-list');
  }
}


