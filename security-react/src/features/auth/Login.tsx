import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { z } from "zod";
import { useAuth } from "../../hooks/useAuth";
import { LoginRequestDTO } from "../../types/auth.types";
import { api } from "../../utils/axios";

const loginSchema = z.object({
    email: z.string().email({ message: "Email inválido" }),
    password: z.string().min(3, { message: "Mínimo 3 caracteres" }),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export default function Login() {
    const navigate = useNavigate();
    const { setUser } = useAuth();

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
    });

    const onSubmit = async (data: LoginFormValues) => {
        try {
            const loginData: LoginRequestDTO = {
                email: data.email,
                password: data.password
            };
    
            await api.post("/auth/login", loginData);
            const res = await api.get("/auth/profile");
            setUser(res.data);
            navigate("/profile");
        } catch (error) {
            console.error("Error al iniciar sesión", error);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100">
            <form
                onSubmit={handleSubmit(onSubmit)}
                className="bg-white p-8 rounded-lg shadow-md w-full max-w-md"
            >
                <h2 className="text-2xl font-bold mb-6 text-center">Iniciar Sesión</h2>

                <div className="mb-4">
                    <label className="block mb-1 font-semibold">Email</label>
                    <input
                        {...register("email")}
                        type="email"
                        className="w-full border rounded p-2"
                        placeholder="correo@ejemplo.com"
                    />
                    {errors.email && (
                        <p className="text-red-500 text-sm mt-1">{errors.email.message}</p>
                    )}
                </div>

                <div className="mb-6">
                    <label className="block mb-1 font-semibold">Contraseña</label>
                    <input
                        {...register("password")}
                        type="password"
                        className="w-full border rounded p-2"
                        placeholder="********"
                    />
                    {errors.password && (
                        <p className="text-red-500 text-sm mt-1">{errors.password.message}</p>
                    )}
                </div>

                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700 transition mb-3"
                >
                    {isSubmitting ? "Ingresando..." : "Ingresar"}
                </button>

                <Link to="/" className="text-blue-600 underline">Go to Home</Link>
            </form>
        </div>
    );
}
