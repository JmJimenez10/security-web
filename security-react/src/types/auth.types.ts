export interface LoginRequestDTO {
  email: string;
  password: string;
}

export interface RegisterRequestDTO {
  name: string;
  surnames: string;
  email: string;
  phone: string;
  password: string;
}

export interface LoggedUserDTO {
  name: string;
  surnames: string;
  email: string;
  phone: string;
  roles: string[];
}
