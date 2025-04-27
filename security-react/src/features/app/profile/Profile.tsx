import { useAuth } from '../../../hooks/useAuth';

export const Profile = () => {
    const { user, logout } = useAuth();

    return (
        <div className="p-4 max-w-md mx-auto">
            <h1 className="text-2xl font-bold mb-4">Perfil de Usuario</h1>
            <div className="mb-4">
            </div>
            <div className="mb-4">
                <strong>Nombre de usuario:</strong> {user?.name}
            </div>
            <div className="mb-4">
                <strong>Correo electrónico:</strong> {user?.email}
            </div>
            <button
                onClick={logout}
                className="bg-red-500 text-white px-4 py-2 rounded mt-4"
            >
                Cerrar sesión
            </button>
        </div>
    );
};
