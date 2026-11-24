pub struct Person {
    pub name: String,
    pub age: u32,    
    pub can_vote: bool,
}

pub fn create_person_struct(name: String, age: u32) -> Person {
    Person {
        name,
        age,
        can_vote: age >= 18,
    }
}

pub fn mutate_person() -> Person {
    let mut user = Person {
        name: String::from("John"),
        age: 35,
        can_vote: true,
    };
    user.name = String::from("Jane");
    user.age = 36;
    user
}

pub fn struct_update_syntax() -> (Person, Person) {
    let person1 = Person {
        name: String::from("Alice"),
        age: 20,
        can_vote: true,
    };

    let person2 = Person {
        name: String::from("Bob"),
        ..person1
    };

    (person1, person2)
}

pub fn struct_with_method() -> Person {
    let mut person = Person {
        name: String::from("Charlie"),
        age: 25,
        can_vote: true,
    };
    person.age += 1;
    person
}