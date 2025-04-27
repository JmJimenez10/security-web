import axios from "axios";

// Crear la instancia de Axios
export const api = axios.create({
  baseURL: "http://localhost:8080/api",
  withCredentials: true, // Importante para enviar las cookies (incluyendo csrf_token)
});

// Función para obtener el valor de una cookie
const getCookie = (name: string) => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) return parts.pop()?.split(';').shift();
  return null;
};

// Interceptor para agregar el CSRF token en las cabeceras
api.interceptors.request.use((config) => {
  // Obtener el CSRF token de las cookies, pero solo si es necesario
  const csrfToken = getCookie('csrf_token');  // Lee la cookie CSRF
  if (csrfToken) {
    config.headers['X-CSRF-Token'] = csrfToken;  // Añade el token a las cabeceras
  }
  return config;
}, (error) => {
  // Manejo de errores en las solicitudes
  return Promise.reject(error);
});

// Interceptor para manejar errores en las respuestas
api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // Si recibimos un error 401 (no autorizado), intentamos refrescar el token
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      // Aquí puedes implementar la lógica para el refresh token si lo tienes configurado
      // Ejemplo:
      // await api.post("/auth/refresh-token");
      // Luego reintentar la solicitud original
    }

    return Promise.reject(error);
  }
);
