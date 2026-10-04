export function errorMessage(err: any): string { 

 if (err?.status === 0)
     return 'Cannot reach the server. Is the Gateway running?';

 const fields = err?.error?.fieldErrors;

 if (fields)
     return Object.values(fields).join(', ');
    
 return err?.error?.message ?? 'Unexpected error';
}
