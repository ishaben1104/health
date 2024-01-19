package com.example.health

object Constants {
    // Arraylist and return the Arraylist
    fun getEmployeeData():ArrayList<Employee>{
        // create an arraylist of type employee class
        val employeeList=ArrayList<Employee>()
        val emp1=Employee("Chinmaya Mohapatra","chinmaya@gmail.com")
        employeeList.add(emp1)
        val emp2=Employee("Ram prakash","ramp@gmail.com")
        employeeList.add(emp2)
        val emp3=Employee("OMM Meheta","mehetaom@gmail.com")
        employeeList.add(emp3)
        val emp4=Employee("Hari Mohapatra","harim@gmail.com")
        employeeList.add(emp4)
        val emp5=Employee("Abhisek Mishra","mishraabhi@gmail.com")
        employeeList.add(emp5)
        val emp6=Employee("Sindhu Malhotra","sindhu@gmail.com")
        employeeList.add(emp6)
        val emp7=Employee("Anil sidhu","sidhuanil@gmail.com")
        employeeList.add(emp7)
        val emp8=Employee("Sachin sinha","sinhas@gmail.com")
        employeeList.add(emp8)
        val emp9=Employee("Amit sahoo","sahooamit@gmail.com")
        employeeList.add(emp9)
        val emp10=Employee("Raj kumar","kumarraj@gmail.com")
        employeeList.add(emp10)

        return  employeeList
    }
}