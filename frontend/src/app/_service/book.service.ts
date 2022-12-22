import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { Book } from '../_model/book.model';
import { Observable } from 'rxjs';
import {UserService} from '../_service/user.service';
import { JwtResponse } from '../_model/jwtResponse.model';
import { Subscription } from '../_model/subscription.model';
import { SubscriptionPayLoad } from '../_model/subscriptionpayload.model';





@Injectable({
  providedIn: 'root'
})
export class BookService {
 
  book:Book;
  currentUser:JwtResponse;
  constructor(private http: HttpClient, private userService:UserService) {
    this.currentUser = this.userService.currentUserValue;
   }


 public getSeachedBooks(category:string, title:string, author:string, price:number,publisher:string ){
  let url = "https://5ij7a2fimi.execute-api.ap-northeast-1.amazonaws.com/UAT/searchBook"
  url=url+"?category="+category+"&title="+title+"&author="+author+"&price="+price+"&publisher="+publisher;
 console.log("url: "+url)
  return this.http.get(url);
  //.pipe(map(response => response));

 }

 public createBooks(logo:File, category:string, title:string, authorId:number, price:number,
  publisher:string, active:boolean, content:string)
{
  let savebookurl= "https://5ij7a2fimi.execute-api.ap-northeast-1.amazonaws.com/UAT/author"
  
  const formdata: FormData = new FormData(); 
  const bookInfo:Book = {
    //logo:logo,
    title: title,
    category: category,
    price:price,
    publisher:publisher,
    active:active,
    content:content
  }; 
  return this.http.post(savebookurl+"/"+authorId+"/books/", bookInfo);
}


public updateBooks(logo:File, category:string, title:string, authorId:number, price:number,
  publisher:string, active:boolean, content:string, bookId:number)
{
  let updatebookurl= "https://5ij7a2fimi.execute-api.ap-northeast-1.amazonaws.com/UAT/author"
  
  const formdata: FormData = new FormData(); 
  const bookInfo:Book = {
    //logo:logo,
    title: title,
    category: category,
    price:price,
    publisher:publisher,
    active:active,
    content:content
  }; 
  return this.http.post(updatebookurl+"/"+authorId+"/books/"+bookId, bookInfo);
}

  public uploadlogo(logo:File) {
    let uploadlogourl="https://5ij7a2fimi.execute-api.ap-northeast-1.amazonaws.com/UAT/uploadlogo"
    
    const formdata: FormData = new FormData();  
    formdata.append('file',logo);
    return this.http.post(uploadlogourl, formdata)
  }

  public subscribe(bookId: number) {
    let subscriptionurl = "https://5ij7a2fimi.execute-api.ap-northeast-1.amazonaws.com/UAT/digitalbooks/";
    let subcribe:SubscriptionPayLoad ={
      bookId:bookId,
      email: this.currentUser.email
    }
    const formdata: FormData = new FormData();  
    formdata.append('bookId',JSON.stringify(bookId));
    formdata.append('email', this.currentUser.email)
    return this.http.post(subscriptionurl+bookId+"/subscribe", subcribe)
  }

  public getAllSubscribedBook(email: string) {

    let subscribedvbookurl = "https://5ij7a2fimi.execute-api.ap-northeast-1.amazonaws.com/UAT/readers/";
    return this.http.get(subscribedvbookurl+email+"/books");
    
  }


  public getSubscriptionIdOfEachBook(userId:number, bookId:number){
    
let subscribedvbookurl = "https://5ij7a2fimi.execute-api.ap-northeast-1.amazonaws.com/UAT/readers/";
    subscribedvbookurl=subscribedvbookurl+userId+"/"+bookId;
   console.log("url: "+subscribedvbookurl)
    return this.http.get(subscribedvbookurl);
    //.pipe(map(response => response));
  
   }
  public unsubscribe(email:string, subscriptionId: number) {
    let subscribedvbookurl = "https://5ij7a2fimi.execute-api.ap-northeast-1.amazonaws.com/UAT/readers/"+email+"/books/"+subscriptionId+"/cancel-subscription"
    return this.http.put(subscribedvbookurl, null);
  }


  public openBook(email:string,subscriptionId: string) {
    let subscribedvbookurl = "https://5ij7a2fimi.execute-api.ap-northeast-1.amazonaws.com/UAT/readers/"+email+"/books/"+subscriptionId+"/read"
    return this.http.get(subscribedvbookurl);
  }

  public getAllCreatedBook(authorId: number) {
    let createdbookurl = "https://5ij7a2fimi.execute-api.ap-northeast-1.amazonaws.com/UAT/createdbook/"+authorId;
    return this.http.get(createdbookurl);
}

public blockBook(userId: number, bookId: number, active:string) {
  let blockbookurl = "https://5ij7a2fimi.execute-api.ap-northeast-1.amazonaws.com/UAT/author/"+userId+"/books/"+bookId+"?block="+active;
  console.log(blockbookurl)
  return this.http.put(blockbookurl, null);
}

getBookById(bookId: number) {
  let fetchbookforupdateurl = "https://5ij7a2fimi.execute-api.ap-northeast-1.amazonaws.com/UAT/book/"+bookId;
  return this.http.get(fetchbookforupdateurl);
}



}
