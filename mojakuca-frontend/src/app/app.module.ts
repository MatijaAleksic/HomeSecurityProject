import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {HttpClientModule} from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { CardComponent } from './card/card.component';
import { HomeComponent } from './home/home.component';
import { HeaderComponent } from './header/header.component';
import { LoginComponent } from './login/login.component';

import {AngularMaterialModule} from './angular-material/angular-material.module';

import {FormsModule, ReactiveFormsModule} from '@angular/forms';

import {ApiService} from './service/api.service';
import {FooService} from './service/foo.service';
import {AuthService} from './service/auth.service';
import {UserService} from './service/user.service';
import {ConfigService} from './service/config.service';

import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { TokenInterceptor } from './interceptor/TokenInterceptor';
import { MyHomesTableComponent } from './my-homes-table/my-homes-table.component';
import { DeviceTableComponent } from './device-table/device-table.component';
import { DeviceMessagesComponent } from './device-messages/device-messages.component';
import { AuthGuard } from './service/auth.guard';
import { RoleGuard } from './service/role.guard';


@NgModule({
  declarations: [
    AppComponent,
    CardComponent,
    HomeComponent,
    HeaderComponent,
    LoginComponent,
    MyHomesTableComponent,
    DeviceTableComponent,
    DeviceMessagesComponent,
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    NoopAnimationsModule,
    AngularMaterialModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  providers: [ 
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true
    },
    FooService,
    AuthService,
    AuthGuard,
    RoleGuard,
    ApiService,
    UserService,
    ConfigService,
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
