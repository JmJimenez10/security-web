import { Link } from "react-router-dom";

export const Home = () => {
  return <div className='bg-neutral-300 text-neutral-800 flex justify-center items-center flex-col h-screen'>
    <h1 className='text-4xl font-bold mb-4'>Home Page</h1>
    <p>Welcome to the home page!</p>
    <p>This is a simple example of a home page component.</p>
    <p>You can add more content here as needed.</p>

    <Link to="/login" className='mt-4 bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600'>
      Go to Login
    </Link>
  </div>
};