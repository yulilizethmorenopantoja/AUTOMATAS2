package compilador;
import java.awt.Color;
import java.awt.image.BandedSampleModel;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.plaf.synth.SynthSpinnerUI;

public class Analisis
{
	int renglon=1;
	ArrayList<String> impresion; 

	final Token vacio=new Token("", 9,0);
	boolean bandera=true,banderaclase=false, banderaErroresSemanticos = false,banderaErroresSintacticos = false;
	public ColorCeldas color = new ColorCeldas(4);
	public String CodigoObjeto=null;
	ArrayList<TabladeSimbolos> tablasimbolos = new ArrayList<TabladeSimbolos>();
	ArrayList<Arbol> arbol = new ArrayList<Arbol>();
	ArrayList<String> expresion = new ArrayList<String>();
	private ArrayList<String> dataCodigo;


	String Anterior1Valor;
	String Anterior2Valor;
	String Anterior3Valor;
	String Anterior4Valor;
	String Anterior5Valor;
	String cadenaauxiliar ="";
	int Anterior1Tipo ;
	int Anterior2Tipo;
	int Anterior3Tipo;
	int Anterior4Tipo;
	int Anterior5Tipo;
	String Siguiente1Valor;
	String Siguiente2Valor;
	int Siguiente1Tipo;	
	String operation = "";
	
	public ArrayList<TabladeSimbolos> getTabla() {
		return tablasimbolos ;
	}
	
	public ArrayList<Arbol> getTabla2() {
		return arbol ;
	}
	
	public ArrayList<String> getTabla3() {
		return dataCodigo ;
	}
	
	public Analisis(String ruta) {//Recibe el nombre del archivo de texto
		analisaCodigo(ruta);
		if(bandera) {
			impresion.add("No hay errores lexicos");
			
			banderaErroresSemanticos = false;
			banderaErroresSintacticos = false;

			if(!banderaclase){
				impresion.add("Falta la inicialización de la clase!");
			}
			
		}
		

		if(!banderaErroresSintacticos)
			impresion.add("No hay errores sintacticos!");
		
		if(!banderaErroresSemanticos)
			impresion.add("No hay errores semanticos!");

		
		for (int i = 0; i < tablasimbolos.size(); i++) {
			System.out.println(tablasimbolos.get(i).toString());
		}
		System.out.println();
			
	}
	public void analisaCodigo(String ruta) {
		String linea="", token="";
		StringTokenizer tokenizer;
		try{
	          FileReader file = new FileReader(ruta);
	          BufferedReader archivoEntrada = new BufferedReader(file);
	          linea = archivoEntrada.readLine();
	          impresion=new ArrayList<String>();

	          
	          while (linea != null){
	        	    linea = separaDelimitadores(linea);
	                tokenizer = new StringTokenizer(linea);
	                while(tokenizer.hasMoreTokens()) {
	                	token = tokenizer.nextToken();
	                	analisisLexico(token);
	                }
	                linea=archivoEntrada.readLine();
	                renglon++;
	          }
	          archivoEntrada.close();
		}catch(IOException e) {
			JOptionPane.showMessageDialog(null,"No se encontro el archivo favor de checar la ruta","Alerta",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	
	public void analisisLexico(String token) {
		
		AppCompilador.eliminarErrorSintactico();
		AppCompilador.eliminarErrorSemantico();
		int tipo=0;
		if(Arrays.asList("public").contains(token)) 
			tipo = Token.MODIFICADOR;
		else if(Arrays.asList("if","else").contains(token)) 
			tipo = Token.PALABRA_RESERVADA;
		else if(Arrays.asList("int","char","float","boolean").contains(token))
			tipo = Token.TIPO_DATO;
		else if(Arrays.asList("(",")","{","}","=",";").contains(token))
			tipo = Token.SIMBOLO;
		else if(Arrays.asList("<",">","==").contains(token))
			tipo = Token.OPERADOR_LOGICO; 
		else if(Arrays.asList("+","-","*","/").contains(token))
			tipo = Token.OPERADOR_ARITMETICO;
		else if(Arrays.asList("True","False").contains(token)||Pattern.matches("^[0-9]+$",token)
				||Pattern.matches("[0-9]+.[0-9]+",token)||Pattern.matches("'[a-zA-Z]'",token) ||Pattern.matches("-[0-9]+$",token)) 
			tipo = Token.CONSTANTE;
		else if(token.equals("class")) 
			tipo =Token.CLASE;
		else {
			//Cadenas validas
			Pattern pat = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*$")  ;//Expresiones Regulares
			Matcher mat = pat.matcher(token);
				
			if(mat.find()) 
				tipo = Token.IDENTIFICADOR;
			
	
			else {
				impresion.add("Error lexico en la linea "+renglon+" token "+token);
				bandera = false;
				return;
			}
		}

		impresion.add(new Token(token,tipo,renglon).toString());
		

		
	}
	

	public void GenerarCodigoObjeto(ArrayList<Arbol> tabla) {
		System.out.println("CODIGO OBJETO");
		dataCodigo = new ArrayList<String>();
		dataCodigo.add("                    .MODEL                   small");
		dataCodigo.add("                    .DATA ");
		//DECLARAR VARIABLES
		for (Arbol item : getTabla2()) {
			dataCodigo.add(item.resultado+"               DW                  0");
		}
		dataCodigo.add("                    .CODE");
		dataCodigo.add("MAIN            PROC                     FAR");
		dataCodigo.add("                    .STARTUP");
		
		for (int i=0; i < getTabla2().size(); i++) {
			Arbol id2 = getTabla2().get(i);								
			System.out.println("Item: "+"[ "+id2.operador+", "+id2.argumento1+", "+id2.argumento2+", "+id2.resultado+" ]");
			if(id2.operador.equals("+")) {
				operation = "ADD";
				dataCodigo.add("                    ;SUMA");
			}else if (id2.operador.equals("-")) {
				operation = "SUB";
				dataCodigo.add("                    ;RESTA");
			}else if (id2.operador.equals("*")) {
				operation = "MUL";
				dataCodigo.add("                    ;MULTIPLICACION");
			}else if (id2.operador.equals("/")) {
				operation = "DIV";
				dataCodigo.add("                    ;DIVISION");
			}else if (id2.operador.equals("=")) {
				dataCodigo.add("                    ;ASIGNACION");
			}
			if(!id2.operador.equals("=") && ( operation.equals("MUL") || operation.equals("DIV") )) {
				dataCodigo.add("                    MOV	 AX,"+id2.argumento1);
				dataCodigo.add("                    MOV	 BX,"+id2.argumento2);
				dataCodigo.add("                    "+operation+" BX");
				dataCodigo.add("                    MOV	 "+id2.resultado+", AX");
			}
			else if(!id2.operador.equals("=") && ( operation.equals("ADD") || operation.equals("SUB") )) {
				dataCodigo.add("                    MOV	 AX,"+id2.argumento1);
				dataCodigo.add("                    MOV	 BX,"+id2.argumento2);
				dataCodigo.add("                    "+operation+" AX, BX");
				dataCodigo.add("                    MOV	 "+id2.resultado+", AX");
			}
			else {
				dataCodigo.add("                    MOV	 AX,"+id2.argumento1);
				dataCodigo.add("                    MOV "+id2.resultado+", AX");
			}
		}
		dataCodigo.add("MAIN            ENDP");
		// IMPRESION
		for (String item : dataCodigo) {
			System.out.println(item);
		}
		
	}


	
	public static boolean EsNumeroEntero(String cadena) {

		boolean resultado;

		try {
			Integer.parseInt(cadena);
			resultado = true;
		} catch (NumberFormatException excepcion) {
			resultado = false;
		}

		return resultado;
	}

	public static boolean Esfloat(String cadena) {

		boolean resultado;

		try {
			Float.parseFloat(cadena);
			resultado = true;
		} catch (NumberFormatException excepcion) {
			resultado = false;
		}

		return resultado;
	}


	public static boolean EsChar(String cadena) {

		boolean resultado;

		if(Pattern.matches("'[a-zA-Z]'",cadena))
			return true;
		return false;

	}


	public static boolean EsBoolean(String cadena) {


		if(cadena.contains("True")||cadena.contains("False"))
			return true;
		return false;

	}


	public static String TipoCadena(String cadena) {

		String resultado= "";

	
		if(Pattern.matches("[0-9]+",cadena)){
			resultado = "int";
			return resultado;
		}

		if(Pattern.matches("[0-9]+.[0-9]+",cadena)){
			resultado = "float";
		}


		if(Pattern.matches("'[a-zA-Z]'",cadena)){
			resultado = "char";
		}

		if(cadena.contains("True")||cadena.contains("False")){
			resultado = "boolean";
		}

		return resultado;
	}



	// por si alguien escribe todo pegado 
	public String separaDelimitadores(String linea){
		for (String string : Arrays.asList("(",")","{","}","=",";")) {
			if(string.equals("=")) {
				
				if(linea.indexOf("==")>=0)
				{
					linea = linea.replace("==", " == ");
					break;
				}
			}
			if(linea.contains(string)) 
				linea = linea.replace(string, " "+string+" ");
		}
		return linea;
	}
	
	
	public ArrayList<String> getmistokens() {
		return impresion;
	}



	public int Sumar (String uno, String dos){

		int suma =0;

		suma = suma+Integer.parseInt(uno)+Integer.parseInt(dos);


		return suma;
	}
	
	public int Restar (String uno, String dos){

		int Resta =0;

		Resta = Resta+Integer.parseInt(uno)-Integer.parseInt(dos);


		return Resta;
	}
	
	public int multiplicar (String uno, String dos){

		int multi =0;

		multi = multi+Integer.parseInt(uno)*Integer.parseInt(dos);


		return multi;
	}
	
	public int dividir (String uno, String dos){

		int div =0;

		div = div+ (int)( Integer.parseInt(uno)/Integer.parseInt(dos));


		return div;
	}
	
	public String getmisObjetos() {
		return CodigoObjeto;
	}
	
	public String jerarquias (int i, String aux){
		
		
		return aux;
	}
	
}
