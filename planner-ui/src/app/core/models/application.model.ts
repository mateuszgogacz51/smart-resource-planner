export interface Application {
  id: number;
  title: string;
  description: string;
  resourceId: number;
  startTime: string; 
  endTime: string;   
  createdAt: string; 
  status: string; // <--- DODAJ TO POLE, aby pasowało do Twojego HTML
  assignedTo?: string; 
  ownerUsername: string; 
  comments?: string[]; 
}