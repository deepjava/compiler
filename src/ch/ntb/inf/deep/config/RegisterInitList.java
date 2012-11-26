package ch.ntb.inf.deep.config;

import ch.ntb.inf.deep.host.ErrorReporter;


public class RegisterInitList implements ErrorCodes {
	
	private RegisterInit regInits;
	private CPU cpu;

	public RegisterInitList() {}
	
	public RegisterInitList(CPU cpu) {
		this.cpu = cpu;
	}
	
	public void setCpu(CPU cpu) {
		this.cpu = cpu;
	}
	
	public void addRegInit(String registerName, int value) {
		if(cpu == null) {
			System.out.println("ERROR, Can not add register init for register " + registerName + " because cpu is not set!"); // TODO improve this
			return;
		}
//		if(cpu.registermap == null) {
//			System.out.println("ERROR, Can not add register init for register " + registerName + " because register map of cpu " + cpu.getName() + " is not set!"); // TODO improve this
//			return;
//		}		
		Register reg = cpu.getRegisterByName(registerName);
		if(reg == null) {
			ErrorReporter.reporter.error(errNoSuchRegister, registerName);
			return;
		}
		if (regInits == null) {
			regInits = new RegisterInit(reg, value);
		}
		else {
			RegisterInit regInit = regInits.getRegisterInitByRegister(reg);
			if(regInit != null) {
				regInit.setInitValue(value);
				System.out.println("[WARNING] Overriding initial value for rgister " + reg.getName() + "!"); // TODO improve this
			}
			else {
				regInits.append(new RegisterInit(reg, value));
			}
		}
	}

	public RegisterInit getFirstRegInit(){
		if(regInits != null) return (RegisterInit)regInits.getHead();
		return null;
	}
}
