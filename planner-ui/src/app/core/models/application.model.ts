export interface Comment {
  id?: number;
  author: string;
  content: string;
  createdAt: string;
}

export interface Application {
  id: number;
  resourceId: number;
  userId: string;      // Musi być userId, żeby pasowało do Javy
  startTime: string; 
  endTime: string;   
  createdAt: string; 
  status: string; 
  comments?: Comment[]; 
}